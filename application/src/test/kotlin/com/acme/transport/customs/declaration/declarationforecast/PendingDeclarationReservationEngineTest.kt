package com.acme.transport.customs.declaration.declarationforecast

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingDeclarationReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingDeclarationReservationEngine(newRegistry())
        val item = newPendingDeclarationReservation("code-799", "label-205")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
