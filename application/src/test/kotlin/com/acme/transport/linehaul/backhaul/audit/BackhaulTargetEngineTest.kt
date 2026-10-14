package com.acme.transport.linehaul.backhaul.audit

import kotlin.test.Test
import kotlin.test.assertTrue

class BackhaulTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBackhaulTargetEngine()
        val item = newBackhaulTarget("code-563", "label-120")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
