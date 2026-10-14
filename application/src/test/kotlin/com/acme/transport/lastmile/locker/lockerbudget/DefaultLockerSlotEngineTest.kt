package com.acme.transport.lastmile.locker.lockerbudget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultLockerSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultLockerSlotEngine(newRegistry())
        val item = newDefaultLockerSlot("code-362", "name-365")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
