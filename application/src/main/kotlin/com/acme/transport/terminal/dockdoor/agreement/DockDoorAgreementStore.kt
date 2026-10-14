package com.acme.transport.terminal.dockdoor.agreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dock door agreements by code. */
interface DockDoorAgreementStore {
    fun find(code: String): DockDoorAgreement?
    fun save(item: DockDoorAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<DockDoorAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDockDoorAgreementStore(
    var items: MutableMap<String, DockDoorAgreement>,
    var order: MutableList<String>,
) : DockDoorAgreementStore {
    override fun find(code: String): DockDoorAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DockDoorAgreement) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<DockDoorAgreement> {
        val out = mutableListOf<DockDoorAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDockDoorAgreementStore(): DockDoorAgreementStore {
    return MemoryDockDoorAgreementStore(mutableMapOf<String, DockDoorAgreement>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDockDoorAgreementStore(
    var inner: DockDoorAgreementStore,
    var cache: TtlCache<DockDoorAgreement>,
    var hits: Int,
    var misses: Int,
) : DockDoorAgreementStore {
    override fun find(code: String): DockDoorAgreement? {
        val hit = (cache.get(code) as DockDoorAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DockDoorAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DockDoorAgreement> = inner.list()
}

fun newCachedDockDoorAgreementStore(inner: DockDoorAgreementStore, ttl: Int, clock: Clock): DockDoorAgreementStore {
    return CachedDockDoorAgreementStore(
        inner = inner,
        cache = newTtlCache<DockDoorAgreement>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dock door agreements. Empty fields match all. */
data class DockDoorAgreementQuery(
    var region: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DockDoorAgreement): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DockDoorAgreement>): MutableList<DockDoorAgreement> {
        val matched = filter(items) { candidate: DockDoorAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDockDoorAgreements(store: DockDoorAgreementStore, query: DockDoorAgreementQuery): MutableList<DockDoorAgreement> {
    return query.apply(store.list())
}
