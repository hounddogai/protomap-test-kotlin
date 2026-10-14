package com.acme.transport.lastmile.locker.tier

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LockerTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLockerTierEngine(newRegistry())
        val item = newLockerTier("code-236", "title-529")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
