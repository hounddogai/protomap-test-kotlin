package com.acme.transport.fleetops.defect.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefectProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefectProfileEngine(newRegistry())
        val item = newDefectProfile("code-185", "label-194")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
