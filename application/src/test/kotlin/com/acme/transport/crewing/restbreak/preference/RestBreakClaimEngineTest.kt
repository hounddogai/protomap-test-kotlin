package com.acme.transport.crewing.restbreak.preference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RestBreakClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRestBreakClaimEngine(newRegistry())
        val item = newRestBreakClaim("code-373", "label-425")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
