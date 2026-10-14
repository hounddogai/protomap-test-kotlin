package com.acme.transport.fleetops.defect.exception

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingDefectExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingDefectExceptionEngine(newRegistry())
        val item = newRollingDefectException("code-599", "title-391")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
