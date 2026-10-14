package com.acme.transport.lastmile.locker.manifest

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingLockerPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingLockerPolicyEngine()
        val item = newRollingLockerPolicy("code-408", "title-959")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
