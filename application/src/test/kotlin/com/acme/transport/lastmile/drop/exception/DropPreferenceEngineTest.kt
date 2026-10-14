package com.acme.transport.lastmile.drop.exception

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DropPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDropPreferenceEngine(newRegistry())
        val item = newDropPreference("code-812", "label-973")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
