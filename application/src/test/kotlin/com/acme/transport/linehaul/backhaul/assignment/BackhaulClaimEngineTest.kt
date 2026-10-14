package com.acme.transport.linehaul.backhaul.assignment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BackhaulClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBackhaulClaimEngine(newRegistry())
        val item = newBackhaulClaim("code-751", "name-191")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
