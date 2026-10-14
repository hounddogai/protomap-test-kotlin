package com.acme.transport.lastmile.locker.lockerclaim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackLockerDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackLockerDraftEngine(newRegistry())
        val item = newFallbackLockerDraft("code-184", "title-624")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
