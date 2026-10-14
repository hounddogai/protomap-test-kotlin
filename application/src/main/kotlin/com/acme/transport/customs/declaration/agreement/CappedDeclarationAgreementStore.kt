package com.acme.transport.customs.declaration.agreement

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped declaration agreements by code. */
interface CappedDeclarationAgreementStore {
    fun find(code: String): CappedDeclarationAgreement?
    fun save(item: CappedDeclarationAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedDeclarationAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedDeclarationAgreementStore(
    var items: MutableMap<String, CappedDeclarationAgreement>,
    var order: MutableList<String>,
) : CappedDeclarationAgreementStore {
    override fun find(code: String): CappedDeclarationAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedDeclarationAgreement) {
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

    override fun list(): MutableList<CappedDeclarationAgreement> {
        val out = mutableListOf<CappedDeclarationAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedDeclarationAgreementStore(): CappedDeclarationAgreementStore {
    return MemoryCappedDeclarationAgreementStore(
        mutableMapOf<String, CappedDeclarationAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedDeclarationAgreementStore(
    var inner: CappedDeclarationAgreementStore,
    var cache: TtlCache<CappedDeclarationAgreement>,
    var hits: Int,
    var misses: Int,
) : CappedDeclarationAgreementStore {
    override fun find(code: String): CappedDeclarationAgreement? {
        val hit = (cache.get(code) as CappedDeclarationAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedDeclarationAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedDeclarationAgreement> = inner.list()
}

fun newCachedCappedDeclarationAgreementStore(inner: CappedDeclarationAgreementStore, ttl: Int, clock: Clock): CappedDeclarationAgreementStore {
    return CachedCappedDeclarationAgreementStore(
        inner = inner,
        cache = newTtlCache<CappedDeclarationAgreement>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped declaration agreements. Empty fields match all. */
data class CappedDeclarationAgreementQuery(
    var title: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedDeclarationAgreement): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedDeclarationAgreement>): MutableList<CappedDeclarationAgreement> {
        val matched = filter(items) { candidate: CappedDeclarationAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedDeclarationAgreements(store: CappedDeclarationAgreementStore, query: CappedDeclarationAgreementQuery): MutableList<CappedDeclarationAgreement> {
    return query.apply(store.list())
}
