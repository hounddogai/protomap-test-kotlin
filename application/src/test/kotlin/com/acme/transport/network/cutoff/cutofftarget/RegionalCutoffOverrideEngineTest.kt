package com.acme.transport.network.cutoff.cutofftarget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalCutoffOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalCutoffOverrideEngine(newRegistry())
        val item = newRegionalCutoffOverride("code-828", "label-496")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
