import { Component, EventEmitter, Input, Output } from "@angular/core";
import { humanizeBackendMessage } from "../core/display-labels";
import { Notification } from "../core/models";

export type NotificationReadFilter = boolean | null;

@Component({
  selector: "app-notification-list",
  standalone: true,
  template: `
    <div class="panel notifications-panel">
      <div class="panel-header notification-header">
        <div>
          <h2>Notificacoes</h2>
          <span>{{ unreadCount }} nao lidas</span>
        </div>

        <button class="ghost" type="button" [disabled]="loading || unreadCount === 0" (click)="markAllAsRead.emit()">
          Marcar todas
        </button>
      </div>

      <div class="segmented-control notification-filter" aria-label="Filtro de notificacoes">
        @for (option of filterOptions; track option.label) {
          <button
            class="ghost"
            type="button"
            [class.active]="readFilter === option.value"
            (click)="readFilterChange.emit(option.value)"
          >
            {{ option.label }}
          </button>
        }
      </div>

      <div class="stack">
        @for (notification of notifications; track notification.id) {
          <article class="compact-card notification-card" [class.unread]="!notification.read">
            <div class="notification-card-header">
              <strong>{{ notification.subject }}</strong>
              <span>{{ notification.read ? "Lida" : "Nao lida" }}</span>
            </div>

            <p>{{ notificationMessage(notification.message) }}</p>

            @if (!notification.read) {
              <button class="ghost" type="button" [disabled]="loading" (click)="markAsRead.emit(notification.id)">
                Marcar lida
              </button>
            }
          </article>
        } @empty {
          <p>Nenhuma notificacao encontrada.</p>
        }
      </div>
    </div>
  `,
})
export class NotificationListComponent {
  @Input({ required: true }) notifications: Notification[] = [];
  @Input() unreadCount = 0;
  @Input() readFilter: NotificationReadFilter = null;
  @Input() loading = false;
  @Output() markAsRead = new EventEmitter<string>();
  @Output() markAllAsRead = new EventEmitter<void>();
  @Output() readFilterChange = new EventEmitter<NotificationReadFilter>();

  readonly filterOptions: Array<{ label: string; value: NotificationReadFilter }> = [
    { label: "Todas", value: null },
    { label: "Nao lidas", value: false },
    { label: "Lidas", value: true },
  ];

  notificationMessage(message: string): string {
    return humanizeBackendMessage(message);
  }
}
