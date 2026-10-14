package com.acme.transport.lastmile.locker.agreement

import kotlin.test.Test
import kotlin.test.assertTrue

class PendingLockerAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingLockerAgreementEngine()
        val item = newPendingLockerAgreement("code-531", "title-290")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
