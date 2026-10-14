package com.acme.transport.customs.declaration.notice

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps declaration notices by code. */
interface DeclarationNoticeStore {
    fun find(code: String): DeclarationNotice?
    fun save(item: DeclarationNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<DeclarationNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDeclarationNoticeStore(
    var items: MutableMap<String, DeclarationNotice>,
    var order: MutableList<String>,
) : DeclarationNoticeStore {
    override fun find(code: String): DeclarationNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DeclarationNotice) {
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

    override fun list(): MutableList<DeclarationNotice> {
        val out = mutableListOf<DeclarationNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDeclarationNoticeStore(): DeclarationNoticeStore {
    return MemoryDeclarationNoticeStore(mutableMapOf<String, DeclarationNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDeclarationNoticeStore(
    var inner: DeclarationNoticeStore,
    var cache: TtlCache<DeclarationNotice>,
    var hits: Int,
    var misses: Int,
) : DeclarationNoticeStore {
    override fun find(code: String): DeclarationNotice? {
        val hit = (cache.get(code) as DeclarationNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DeclarationNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DeclarationNotice> = inner.list()
}

fun newCachedDeclarationNoticeStore(inner: DeclarationNoticeStore, ttl: Int, clock: Clock): DeclarationNoticeStore {
    return CachedDeclarationNoticeStore(
        inner = inner,
        cache = newTtlCache<DeclarationNotice>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects declaration notices. Empty fields match all. */
data class DeclarationNoticeQuery(
    var country: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DeclarationNotice): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DeclarationNotice>): MutableList<DeclarationNotice> {
        val matched = filter(items) { candidate: DeclarationNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDeclarationNotices(store: DeclarationNoticeStore, query: DeclarationNoticeQuery): MutableList<DeclarationNotice> {
    return query.apply(store.list())
}
