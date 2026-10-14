package com.acme.transport.linehaul.laneplan.window

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedLanePlanTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedLanePlanTargetEngine(newRegistry())
        val item = newCappedLanePlanTarget("code-213", "label-434")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
