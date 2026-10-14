package com.acme.transport.network.cutoff.globalcutoffledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps default cutoff agreements by code. */
interface DefaultCutoffAgreementStore {
    fun find(code: String): DefaultCutoffAgreement?
    fun save(item: DefaultCutoffAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultCutoffAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultCutoffAgreementStore(
    var items: MutableMap<String, DefaultCutoffAgreement>,
    var order: MutableList<String>,
) : DefaultCutoffAgreementStore {
    override fun find(code: String): DefaultCutoffAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultCutoffAgreement) {
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

    override fun list(): MutableList<DefaultCutoffAgreement> {
        val out = mutableListOf<DefaultCutoffAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultCutoffAgreementStore(): DefaultCutoffAgreementStore {
    return MemoryDefaultCutoffAgreementStore(
        mutableMapOf<String, DefaultCutoffAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultCutoffAgreementStore(
    var inner: DefaultCutoffAgreementStore,
    var cache: TtlCache<DefaultCutoffAgreement>,
    var hits: Int,
    var misses: Int,
) : DefaultCutoffAgreementStore {
    override fun find(code: String): DefaultCutoffAgreement? {
        val hit = (cache.get(code) as DefaultCutoffAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultCutoffAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultCutoffAgreement> = inner.list()
}

fun newCachedDefaultCutoffAgreementStore(inner: DefaultCutoffAgreementStore, ttl: Int, clock: Clock): DefaultCutoffAgreementStore {
    return CachedDefaultCutoffAgreementStore(
        inner = inner,
        cache = newTtlCache<DefaultCutoffAgreement>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default cutoff agreements. Empty fields match all. */
data class DefaultCutoffAgreementQuery(
    var depot: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultCutoffAgreement): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultCutoffAgreement>): MutableList<DefaultCutoffAgreement> {
        val matched = filter(items) { candidate: DefaultCutoffAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultCutoffAgreements(store: DefaultCutoffAgreementStore, query: DefaultCutoffAgreementQuery): MutableList<DefaultCutoffAgreement> {
    return query.apply(store.list())
}
