package com.acme.transport.linehaul.tender.template

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryTenderPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryTenderPreferenceEngine(newRegistry())
        val item = newPrimaryTenderPreference("code-317", "title-58")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
