package com.acme.transport.linehaul.trailer.budget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingTrailerAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingTrailerAssignmentEngine(newRegistry())
        val item = newRollingTrailerAssignment("code-714", "name-281")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
