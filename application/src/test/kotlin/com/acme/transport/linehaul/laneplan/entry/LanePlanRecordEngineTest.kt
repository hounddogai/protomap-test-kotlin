package com.acme.transport.linehaul.laneplan.entry

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LanePlanRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLanePlanRecordEngine(newRegistry())
        val item = newLanePlanRecord("code-288", "title-611")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
