package com.acme.transport.network.cutoff.cutofftarget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualCutoffDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualCutoffDraftEngine(newRegistry())
        val item = newManualCutoffDraft("code-389", "label-128")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
