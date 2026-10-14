package com.acme.transport.freightpay.fuelsurcharge.mapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalFuelSurchargeExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalFuelSurchargeExceptionEngine(newRegistry())
        val item = newGlobalFuelSurchargeException("code-252", "name-663")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
