package com.acme.transport.lastmile.locker.budget

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledLockerAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledLockerAgreementEngine()
        val item = newScheduledLockerAgreement("code-500", "title-694")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
