package com.acme.transport.customs.hold.profile

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyHoldProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyHoldProfileEngine(newRegistry())
        val item = newLegacyHoldProfile("code-235", "name-26")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
