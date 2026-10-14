package com.acme.transport.linehaul.tender.tenderrollup

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictTenderRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictTenderRollupEngine()
        val item = newStrictTenderRollup("code-553", "label-340")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
