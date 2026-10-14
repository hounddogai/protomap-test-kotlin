package com.acme.transport.customs.hold.digest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedHoldEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedHoldEntryEngine(newRegistry())
        val item = newCappedHoldEntry("code-950", "title-140")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
