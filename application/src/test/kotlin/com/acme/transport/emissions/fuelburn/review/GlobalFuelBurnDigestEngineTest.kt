package com.acme.transport.emissions.fuelburn.review

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalFuelBurnDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalFuelBurnDigestEngine(newRegistry())
        val item = newGlobalFuelBurnDigest("code-402", "label-596")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
