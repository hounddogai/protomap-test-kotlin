package com.acme.transport.terminal.crossdock.slot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps cross dock agreements by code. */
interface CrossDockAgreementStore {
    fun find(code: String): CrossDockAgreement?
    fun save(item: CrossDockAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<CrossDockAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCrossDockAgreementStore(
    var items: MutableMap<String, CrossDockAgreement>,
    var order: MutableList<String>,
) : CrossDockAgreementStore {
    override fun find(code: String): CrossDockAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CrossDockAgreement) {
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

    override fun list(): MutableList<CrossDockAgreement> {
        val out = mutableListOf<CrossDockAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCrossDockAgreementStore(): CrossDockAgreementStore {
    return MemoryCrossDockAgreementStore(mutableMapOf<String, CrossDockAgreement>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCrossDockAgreementStore(
    var inner: CrossDockAgreementStore,
    var cache: TtlCache<CrossDockAgreement>,
    var hits: Int,
    var misses: Int,
) : CrossDockAgreementStore {
    override fun find(code: String): CrossDockAgreement? {
        val hit = (cache.get(code) as CrossDockAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CrossDockAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CrossDockAgreement> = inner.list()
}

fun newCachedCrossDockAgreementStore(inner: CrossDockAgreementStore, ttl: Int, clock: Clock): CrossDockAgreementStore {
    return CachedCrossDockAgreementStore(
        inner = inner,
        cache = newTtlCache<CrossDockAgreement>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cross dock agreements. Empty fields match all. */
data class CrossDockAgreementQuery(
    var source: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CrossDockAgreement): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CrossDockAgreement>): MutableList<CrossDockAgreement> {
        val matched = filter(items) { candidate: CrossDockAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCrossDockAgreements(store: CrossDockAgreementStore, query: CrossDockAgreementQuery): MutableList<CrossDockAgreement> {
    return query.apply(store.list())
}
