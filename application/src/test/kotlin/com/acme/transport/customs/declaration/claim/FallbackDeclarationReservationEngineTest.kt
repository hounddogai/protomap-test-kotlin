package com.acme.transport.customs.declaration.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackDeclarationReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackDeclarationReservationEngine(newRegistry())
        val item = newFallbackDeclarationReservation("code-807", "name-202")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
