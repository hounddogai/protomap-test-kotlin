package com.acme.transport.customs.declaration.milestone

import kotlin.test.Test
import kotlin.test.assertTrue

class DeclarationReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDeclarationReservationEngine()
        val item = newDeclarationReservation("code-422", "label-321")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
