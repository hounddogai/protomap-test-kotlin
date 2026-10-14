package com.acme.transport.lastmile.locker.forecast

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultLockerDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultLockerDigestEngine(newRegistry())
        val item = newDefaultLockerDigest("code-142", "name-750")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
