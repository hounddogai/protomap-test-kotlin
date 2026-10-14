package com.acme.transport.lastmile.locker.lockerassignment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveLockerAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveLockerAssignmentEngine(newRegistry())
        val item = newAdaptiveLockerAssignment("code-19", "name-939")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
