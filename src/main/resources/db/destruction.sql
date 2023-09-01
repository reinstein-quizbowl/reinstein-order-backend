drop view packet_exposure;
drop table invoice_line;
drop table booking_practice_compilation_order;
drop table booking_practice_packet_order;
drop table booking_practice_state_series_order;
drop table non_conference_game_school;
drop table non_conference_game;
drop table booking_conference_packet;
drop table booking_conference_school;
drop table booking_conference;
drop table booking;
drop table booking_status;
drop table school;
drop table compilation;
drop table packet;
drop table state_series;
drop table year;
-- account is not dropped because we generally don't want to re-create it in dev cycles, and no other tables depend on it
