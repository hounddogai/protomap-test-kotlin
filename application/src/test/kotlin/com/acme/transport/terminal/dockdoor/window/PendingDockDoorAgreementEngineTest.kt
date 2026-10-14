package com.acme.transport.terminal.dockdoor.window

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingDockDoorAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingDockDoorAgreementEngine(newRegistry())
        val item = newPendingDockDoorAgreement("code-68", "label-518")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
