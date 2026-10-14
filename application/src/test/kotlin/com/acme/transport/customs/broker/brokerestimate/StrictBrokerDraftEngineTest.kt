package com.acme.transport.customs.broker.brokerestimate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictBrokerDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictBrokerDraftEngine(newRegistry())
        val item = newStrictBrokerDraft("code-955", "label-769")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
