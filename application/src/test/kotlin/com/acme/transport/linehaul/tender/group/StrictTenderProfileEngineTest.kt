package com.acme.transport.linehaul.tender.group

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictTenderProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictTenderProfileEngine(newRegistry())
        val item = newStrictTenderProfile("code-849", "label-856")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
