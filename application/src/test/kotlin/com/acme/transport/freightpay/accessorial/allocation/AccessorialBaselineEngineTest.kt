package com.acme.transport.freightpay.accessorial.allocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AccessorialBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAccessorialBaselineEngine(newRegistry())
        val item = newAccessorialBaseline("code-3", "title-431")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
