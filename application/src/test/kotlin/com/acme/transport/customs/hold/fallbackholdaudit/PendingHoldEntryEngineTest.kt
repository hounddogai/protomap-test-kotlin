package com.acme.transport.customs.hold.fallbackholdaudit

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingHoldEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingHoldEntryEngine(newRegistry())
        val item = newPendingHoldEntry("code-343", "title-210")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
