package com.acme.transport.network.servicearea.agreement

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ServiceAreaDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServiceAreaDraftEngine(newRegistry())
        val item = newServiceAreaDraft("code-350", "name-322")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
