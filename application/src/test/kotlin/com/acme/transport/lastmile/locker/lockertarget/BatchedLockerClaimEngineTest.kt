package com.acme.transport.lastmile.locker.lockertarget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedLockerClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedLockerClaimEngine(newRegistry())
        val item = newBatchedLockerClaim("code-543", "name-245")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
