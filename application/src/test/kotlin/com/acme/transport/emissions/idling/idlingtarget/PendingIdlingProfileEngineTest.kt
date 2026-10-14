package com.acme.transport.emissions.idling.idlingtarget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingIdlingProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingIdlingProfileEngine(newRegistry())
        val item = newPendingIdlingProfile("code-930", "name-629")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
