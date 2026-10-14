package com.acme.transport.customs.hold.fallbackholdaudit

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualHoldWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualHoldWindowEngine(newRegistry())
        val item = newManualHoldWindow("code-13", "name-767")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
