package com.acme.transport.linehaul.backhaul.preference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BackhaulEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBackhaulEstimateEngine(newRegistry())
        val item = newBackhaulEstimate("code-743", "name-483")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
