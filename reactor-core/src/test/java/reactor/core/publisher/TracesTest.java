/*
 * Copyright (c) 2018-2024 VMware Inc. or its affiliates, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package reactor.core.publisher;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TracesTest {

	@Test
	public void extractOperatorLine_reactor() {
		String stack = "\treactor.core.publisher.Flux.filter(Flux.java:4209)\n" +
				"\treactor.core.ScannableTest.operatorChainWithDebugMode(ScannableTest.java:542)\n";

		assertThat(Traces.extractOperatorAssemblyInformation(stack))
				.isEqualTo("Flux.filter ⇢ at reactor.core.ScannableTest.operatorChainWithDebugMode(ScannableTest.java:542)");
	}

	@Test
	public void extractOperatorLine_reactorApiOnly() {
		String stack = "\treactor.core.publisher.Flux.filter(Flux.java:4209)\n" +
				"\treactor.core.publisher.Flux.map(Flux.java:4209)\n";

		assertThat(Traces.extractOperatorAssemblyInformation(stack))
				.isEqualTo("Flux.map(Flux.java:4209)");
	}

	@Test
	public void extractOperatorLine_reactorAliases() {
		String stack = "\treactor.core.publisher.Flux.concatMap(Flux.java:3071)\n"
				+ "\treactor.core.publisher.Flux.concatMap(Flux.java:3036)\n"
				+ "\treactor.core.publisher.Flux.delayUntil(Flux.java:3388)\n"
				+ "\treactor.core.publisher.Flux.delayElements(Flux.java:3314)\n"
				+ "\treactor.core.publisher.Flux.delayElements(Flux.java:3298)\n"
				+ "\treactor.core.ScannableTest.operatorChainWithDebugMode(ScannableTest.java:543)";

		assertThat(Traces.extractOperatorAssemblyInformation(stack))
				.isEqualTo("Flux.delayElements ⇢ at reactor.core.ScannableTest.operatorChainWithDebugMode(ScannableTest.java:543)");
	}

	@Test
	public void extractOperatorLine_userCodeOnly() {
		String stack = "\treactor.core.notPublisher.Flux.filter(Flux.java:4209)\n" +
				"\treactor.core.ScannableTest.operatorChainWithDebugMode(ScannableTest.java:542)\n";

		assertThat(Traces.extractOperatorAssemblyInformation(stack))
				.isEqualTo("reactor.core.notPublisher.Flux.filter(Flux.java:4209)");
	}

	@Test
	public void extractOperatorLine_reactorTest() {
		String stack = "\treactor.core.publisher.Flux.concatMap(Flux.java:3071)\n"
				+ "\treactor.core.publisher.Flux.concatMap(Flux.java:3036)\n"
				+ "\treactor.core.publisher.Flux.delayUntil(Flux.java:3388)\n"
				+ "\treactor.core.publisher.FluxTest.delayElements(FluxTest.java:22)\n"
				+ "\treactor.core.ScannableTest.operatorChainWithDebugMode(ScannableTest.java:543)";

		assertThat(Traces.extractOperatorAssemblyInformation(stack))
				.isEqualTo("Flux.delayUntil ⇢ at reactor.core.publisher.FluxTest.delayElements(FluxTest.java:22)");
	}

	@Test
	public void extractOperatorLine_empty() {
		String stack = "\t\n";

		assertThat(Traces.extractOperatorAssemblyInformation(stack))
				.isEqualTo("[no operator assembly information]");
	}

	@Test
	public void extractOperatorLine_singleIsApi() {
		String stack = "\treactor.core.publisher.Flux.concatMap(Flux.java:3071)\n";

		assertThat(Traces.extractOperatorAssemblyInformation(stack))
				.isEqualTo("Flux.concatMap(Flux.java:3071)");
	}

	@Test
	public void extractOperatorLine_singleIsUserCode() {
		String stack = "\treactor.notcore.publisher.Flux.concatMap(Flux.java:3071)\n";

		assertThat(Traces.extractOperatorAssemblyInformation(stack))
				.isEqualTo("reactor.notcore.publisher.Flux.concatMap(Flux.java:3071)");
	}

	@Test
	public void extractOperatorLine_severalEmptyThenValued() {
		String stack = "    "
				+ "\n"
				+ "\n   "
				+ "\t"
				+ "\t   "
				+ "\t\n"
				+ "\t  \n  "
				+ "\treactor.foo.Bar.baz3(Bar.java:789)\n";

		assertThat(Traces.extractOperatorAssemblyInformation(stack))
				.isEqualTo("reactor.foo.Bar.baz3(Bar.java:789)");
	}

	@Test
	public void extractOperatorLine_severalEmptyThenSeveralValues() {
		String stack = "    "
				+ "\n"
				+ "\n   "
				+ "\t"
				+ "\t   "
				+ "\t\n"
				+ "\t  \n  "
				+ "\treactor.core.publisher.Flux.concatMap(Flux.java:3071)\n"
				+ "\treactor.core.publisher.Flux.concatMap(Flux.java:3036)\n"
				+ "\treactor.core.publisher.Flux.delayUntil(Flux.java:3388)\n"
				+ "\treactor.core.publisher.Flux.delayElements(Flux.java:3314)\n"
				+ "\treactor.foo.Bar.baz(Bar.java:123)\n"
				+ "\treactor.foo.Bar.baz2(Bar.java:456)\n"
				+ "\treactor.foo.Bar.baz3(Bar.java:789)\n";

		assertThat(Traces.extractOperatorAssemblyInformation(stack))
				.isEqualTo("Flux.delayElements ⇢ at reactor.foo.Bar.baz(Bar.java:123)");
	}

	@Test
	public void shouldSanitizeTrue() {
		assertThat(Traces.shouldSanitize("java.util.function")).isTrue();
		assertThat(Traces.shouldSanitize("reactor.core.publisher.Mono.onAssembly")).isTrue();
		assertThat(Traces.shouldSanitize("reactor.core.publisher.Flux.onAssembly")).isTrue();
		assertThat(Traces.shouldSanitize("reactor.core.publisher.ParallelFlux.onAssembly")).isTrue();
		assertThat(Traces.shouldSanitize("reactor.core.publisher.SignalLogger")).isTrue();
		assertThat(Traces.shouldSanitize("reactor.core.publisher.Hooks")).isTrue();
		assertThat(Traces.shouldSanitize("sun.reflect")).isTrue();
		assertThat(Traces.shouldSanitize("java.lang.reflect")).isTrue();
	}

	@Test
	void stackLineViewSanityTest() {
		String stackLine = "\treactor.core.publisher.Flux.filter(Flux.java:4209)\n";

		int end = stackLine.indexOf('\n');
		if (end == -1) {
			System.out.println("No end-of-line");
			end = stackLine.length();
		}

		Traces.StackLineView view = new Traces.StackLineView(stackLine, 0, end)
				.trim();

		assertThat(view.toString()).isEqualTo(stackLine.trim());
		assertThat(view.isEmpty()).isFalse();
		assertThat(view.isUserCode()).isFalse();
		assertThat(view.contains("Flux.filter")).isTrue();
		assertThat(view.startsWith("reactor.core.publisher.Flux")).isTrue();
	}

	@Test
	void stackLineViewLimitsAreCheckedAtStart() {
		String stackLine = "\treactor.core.publisher.Flux.filter(Flux.java:4209)\n";

		Traces.StackLineView incompleteView =
				new Traces.StackLineView(stackLine, stackLine.length() / 2, stackLine.length())
						.trim();

		assertThat(incompleteView.toString()).isEqualTo("ux.filter(Flux.java:4209)");
		assertThat(incompleteView.contains(".filter")).isTrue();
		assertThat(incompleteView.contains("ux.f")).isTrue();
		assertThat(incompleteView.contains("lux.f")).isFalse();
		assertThat(incompleteView.contains("09)")).isTrue();
		assertThat(incompleteView.startsWith("ux.")).isTrue();
		assertThat(incompleteView.startsWith("lux.")).isFalse();
		assertThat(incompleteView.startsWith("ux.filter(Flux.java:4209)")).isTrue();
		assertThat(incompleteView.startsWith("lux.filter(Flux.java:4209)")).isFalse();
	}

	@Test
	void stackLineViewLimitsAreCheckedAtEnd() {
		String stackLine = "\treactor.core.publisher.Flux.filter(Flux.java:4209)\n";

		Traces.StackLineView incompleteView =
				new Traces.StackLineView(stackLine, 0, stackLine.length() / 2)
						.trim();

		assertThat(incompleteView.toString()).isEqualTo("reactor.core.publisher.Fl");
		assertThat(incompleteView.contains("Fl")).isTrue();
		assertThat(incompleteView.contains("Flu")).isFalse();
		assertThat(incompleteView.startsWith("reactor.core.publisher.Fl")).isTrue();
		assertThat(incompleteView.startsWith("reactor.core.publisher.Flux")).isFalse();
	}

	@Test
	void stackLineViewLocationSuffixGetsRemoved() {
		String stackLine = "\treactor.core.publisher.Flux.filter(Flux.java:4209)\n";

		Traces.StackLineView view =
				new Traces.StackLineView(stackLine, 0, stackLine.length()).trim();

		assertThat(view.withoutLocationSuffix()
		               .toString()).isEqualTo("reactor.core.publisher.Flux.filter");
	}

	@Test
	void stackLineViewPublisherPackagePrefixGetsRemoved() {
		String stackLine = "\treactor.core.publisher.Flux.filter(Flux.java:4209)\n";

		Traces.StackLineView view =
				new Traces.StackLineView(stackLine, 0, stackLine.length()).trim();

		assertThat(view.withoutPublisherPackagePrefix()
		               .toString()).isEqualTo("Flux.filter(Flux.java:4209)");
	}
}
