package com.acme.transport.crewing.qualification.reservation

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalQualificationExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalQualificationExceptionEngine()
        val item = newRegionalQualificationException("code-159", "label-854")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
