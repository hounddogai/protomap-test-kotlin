package com.acme.transport.fleetops.defect.checkpoint

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefectExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefectExceptionEngine(newRegistry())
        val item = newDefectException("code-851", "name-798")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
