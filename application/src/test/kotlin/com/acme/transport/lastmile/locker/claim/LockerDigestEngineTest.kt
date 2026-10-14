package com.acme.transport.lastmile.locker.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LockerDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLockerDigestEngine(newRegistry())
        val item = newLockerDigest("code-654", "title-112")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
