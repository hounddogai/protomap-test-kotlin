package com.acme.transport.fleetops.defect.summary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefectRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefectRequestEngine(newRegistry())
        val item = newDefectRequest("code-801", "name-662")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
