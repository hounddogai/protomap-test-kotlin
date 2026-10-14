package com.acme.transport.freightpay.audit.budget

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps audit drafts by code. */
interface AuditDraftStore {
    fun find(code: String): AuditDraft?
    fun save(item: AuditDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<AuditDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAuditDraftStore(
    var items: MutableMap<String, AuditDraft>,
    var order: MutableList<String>,
) : AuditDraftStore {
    override fun find(code: String): AuditDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AuditDraft) {
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

    override fun list(): MutableList<AuditDraft> {
        val out = mutableListOf<AuditDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAuditDraftStore(): AuditDraftStore {
    return MemoryAuditDraftStore(mutableMapOf<String, AuditDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAuditDraftStore(
    var inner: AuditDraftStore,
    var cache: TtlCache<AuditDraft>,
    var hits: Int,
    var misses: Int,
) : AuditDraftStore {
    override fun find(code: String): AuditDraft? {
        val hit = (cache.get(code) as AuditDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AuditDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AuditDraft> = inner.list()
}

fun newCachedAuditDraftStore(inner: AuditDraftStore, ttl: Int, clock: Clock): AuditDraftStore {
    return CachedAuditDraftStore(
        inner = inner,
        cache = newTtlCache<AuditDraft>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects audit drafts. Empty fields match all. */
data class AuditDraftQuery(
    var name: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AuditDraft): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<AuditDraft>): MutableList<AuditDraft> {
        val matched = filter(items) { candidate: AuditDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAuditDrafts(store: AuditDraftStore, query: AuditDraftQuery): MutableList<AuditDraft> {
    return query.apply(store.list())
}
