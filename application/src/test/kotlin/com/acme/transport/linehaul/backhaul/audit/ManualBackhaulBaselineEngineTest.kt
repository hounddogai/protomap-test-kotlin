package com.acme.transport.linehaul.backhaul.audit

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualBackhaulBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualBackhaulBaselineEngine(newRegistry())
        val item = newManualBackhaulBaseline("code-509", "label-586")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
