package com.acme.transport.freightpay.accessorial.score

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalAccessorialScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalAccessorialScoreEngine(newRegistry())
        val item = newGlobalAccessorialScore("code-498", "label-857")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
