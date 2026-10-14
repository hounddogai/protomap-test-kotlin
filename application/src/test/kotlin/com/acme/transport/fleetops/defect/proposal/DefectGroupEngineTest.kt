package com.acme.transport.fleetops.defect.proposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefectGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefectGroupEngine(newRegistry())
        val item = newDefectGroup("code-17", "title-668")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
