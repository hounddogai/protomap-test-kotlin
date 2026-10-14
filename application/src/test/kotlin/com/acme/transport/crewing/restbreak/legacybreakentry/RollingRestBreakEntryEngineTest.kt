package com.acme.transport.crewing.restbreak.legacybreakentry

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingRestBreakEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingRestBreakEntryEngine(newRegistry())
        val item = newRollingRestBreakEntry("code-46", "label-716")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
