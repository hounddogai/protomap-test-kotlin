package com.acme.transport.fleetops.inspection.score

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryInspectionExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryInspectionExceptionEngine()
        val item = newPrimaryInspectionException("code-114", "label-578")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
